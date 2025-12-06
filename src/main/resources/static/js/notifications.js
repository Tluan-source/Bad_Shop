/**
 * Notifications Page JavaScript
 * Handles notification interactions and updates
 */

// Get CSRF token from meta tags
function getCsrfToken() {
     const token = document.querySelector('meta[name="_csrf"]');
     return token ? token.getAttribute('content') : null;
}

function getCsrfHeader() {
     const header = document.querySelector('meta[name="_csrf_header"]');
     return header ? header.getAttribute('content') : 'X-CSRF-TOKEN';
}

document.addEventListener("DOMContentLoaded", function () {
     console.log("Notifications.js loaded");

     // Mark single notification as read
     const markReadButtons = document.querySelectorAll(".mark-read-btn");
     markReadButtons.forEach((button) => {
          button.addEventListener("click", function (e) {
               e.stopPropagation(); // Prevent notification click event
               const notificationId = this.getAttribute("data-id");
               markNotificationAsRead(notificationId);
          });
     });

     // Mark all notifications as read
     const markAllReadBtn = document.getElementById("markAllReadBtn");
     if (markAllReadBtn) {
          markAllReadBtn.addEventListener("click", function () {
               markAllNotificationsAsRead();
          });
     }

     // Click on notification item to navigate and mark as read
     const notificationItems = document.querySelectorAll(".notification-item");
     notificationItems.forEach((item) => {
          item.addEventListener("click", function () {
               const notificationId = this.getAttribute("data-id");
               const notificationType = this.getAttribute("data-type");
               const relatedId = this.getAttribute("data-related-id");
               const isRead = !this.classList.contains("unread");

               // Mark as read if not already read
               if (!isRead) {
                    markNotificationAsRead(notificationId, function () {
                         // Navigate after marking as read
                         navigateToRelatedContent(notificationType, relatedId);
                    });
               } else {
                    // Navigate directly if already read
                    navigateToRelatedContent(notificationType, relatedId);
               }
          });
     });
});

/**
 * Mark a single notification as read
 */
function markNotificationAsRead(notificationId, callback) {
     console.log("Marking notification as read:", notificationId);

     const csrfToken = getCsrfToken();
     const csrfHeader = getCsrfHeader();
     const headers = {
          "Content-Type": "application/json",
     };
     
     if (csrfToken) {
          headers[csrfHeader] = csrfToken;
     }

     fetch(`/notifications/${notificationId}/read`, {
          method: "POST",
          headers: headers,
     })
          .then((response) => {
               if (!response.ok) {
                    throw new Error("Network response was not ok");
               }
               return response.json();
          })
          .then((data) => {
               console.log("Notification marked as read:", data);

               // Update UI
               const notificationItem = document.querySelector(`[data-id="${notificationId}"]`);
               if (notificationItem) {
                    notificationItem.classList.remove("unread");
                    const unreadDot = notificationItem.querySelector(".unread-dot");
                    const markReadBtn = notificationItem.querySelector(".mark-read-btn");
                    if (unreadDot) unreadDot.remove();
                    if (markReadBtn) markReadBtn.remove();
               }

               // Update unread count
               updateUnreadCount(data.unreadCount);

               // Execute callback if provided
               if (callback) callback();
          })
          .catch((error) => {
               console.error("Error marking notification as read:", error);
               showToast("Có lỗi xảy ra khi đánh dấu thông báo", "error");
          });
}

/**
 * Mark all notifications as read
 */
function markAllNotificationsAsRead() {
     console.log("Marking all notifications as read");

     const csrfToken = getCsrfToken();
     const csrfHeader = getCsrfHeader();
     const headers = {
          "Content-Type": "application/json",
     };
     
     if (csrfToken) {
          headers[csrfHeader] = csrfToken;
     }

     fetch("/notifications/read-all", {
          method: "POST",
          headers: headers,
     })
          .then((response) => {
               if (!response.ok) {
                    throw new Error("Network response was not ok");
               }
               return response.json();
          })
          .then((data) => {
               console.log("All notifications marked as read:", data);

               // Update UI - remove all unread states
               const unreadItems = document.querySelectorAll(".notification-item.unread");
               unreadItems.forEach((item) => {
                    item.classList.remove("unread");
                    const unreadDot = item.querySelector(".unread-dot");
                    const markReadBtn = item.querySelector(".mark-read-btn");
                    if (unreadDot) unreadDot.remove();
                    if (markReadBtn) markReadBtn.remove();
               });

               // Hide mark all button and unread badge
               const markAllBtn = document.getElementById("markAllReadBtn");
               const unreadBadge = document.querySelector(".unread-badge");
               if (markAllBtn) markAllBtn.style.display = "none";
               if (unreadBadge) unreadBadge.style.display = "none";

               // Update unread count
               updateUnreadCount(0);

               showToast("Đã đánh dấu tất cả thông báo là đã đọc", "success");
          })
          .catch((error) => {
               console.error("Error marking all notifications as read:", error);
               showToast("Có lỗi xảy ra khi đánh dấu thông báo", "error");
          });
}

/**
 * Navigate to related content based on notification type
 */
function navigateToRelatedContent(type, relatedId) {
     console.log("Navigating to:", type, relatedId);

     if (!relatedId) {
          console.log("No related ID, staying on notifications page");
          return;
     }

     let url = "";
     switch (type) {
          case "ORDER":
               url = `/orders/${relatedId}`;
               break;
          case "PRODUCT":
               url = `/product/${relatedId}`;
               break;
          case "STORE":
               url = `/store/${relatedId}`;
               break;
          case "REVIEW":
               url = `/orders`; // Navigate to orders page for reviews
               break;
          case "VOUCHER":
               url = `/vouchers`;
               break;
          case "SYSTEM":
               // Stay on notifications page for system notifications
               return;
          default:
               console.log("Unknown notification type:", type);
               return;
     }

     if (url) {
          window.location.href = url;
     }
}

/**
 * Update unread notification count in header
 */
function updateUnreadCount(count) {
     // Update badge in header (if exists)
     const headerBadge = document.querySelector(".header-notification-badge");
     if (headerBadge) {
          if (count > 0) {
               headerBadge.textContent = count > 99 ? "99+" : count;
               headerBadge.style.display = "inline-block";
          } else {
               headerBadge.style.display = "none";
          }
     }

     // Update page unread badge
     const pageUnreadBadge = document.querySelector(".unread-badge span");
     if (pageUnreadBadge) {
          pageUnreadBadge.textContent = count;
     }
}

/**
 * Show toast notification
 */
function showToast(message, type = "info") {
     // Check if toast container exists, if not create it
     let toastContainer = document.querySelector(".toast-container");
     if (!toastContainer) {
          toastContainer = document.createElement("div");
          toastContainer.className = "toast-container";
          toastContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
        `;
          document.body.appendChild(toastContainer);
     }

     // Create toast element
     const toast = document.createElement("div");
     toast.className = `toast-notification toast-${type}`;
     toast.style.cssText = `
        background: ${type === "success" ? "#28a745" : type === "error" ? "#dc3545" : "#007bff"};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        margin-bottom: 10px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideInRight 0.3s ease;
        min-width: 250px;
    `;
     toast.textContent = message;

     // Add to container
     toastContainer.appendChild(toast);

     // Auto remove after 3 seconds
     setTimeout(() => {
          toast.style.animation = "slideOutRight 0.3s ease";
          setTimeout(() => {
               toast.remove();
          }, 300);
     }, 3000);
}

// Add animation styles
if (!document.getElementById("toast-animations")) {
     const style = document.createElement("style");
     style.id = "toast-animations";
     style.textContent = `
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
     document.head.appendChild(style);
}
