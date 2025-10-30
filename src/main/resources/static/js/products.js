// 👉 Switch Grid / List View
function switchView(view) {
     const container = document.getElementById("productsContainer");
     const items = document.querySelectorAll(".product-item");
     const gridBtn = document.getElementById("gridViewBtn");
     const listBtn = document.getElementById("listViewBtn");

     if (view === "list") {
          container.classList.remove("row", "g-4");
          container.classList.add("list-view");
          items.forEach((item) => {
               item.classList.remove("col-lg-4", "col-md-6");
               item.classList.add("col-12");
          });

          gridBtn.classList.remove("active");
          listBtn.classList.add("active");
     } else {
          container.classList.remove("list-view");
          container.classList.add("row", "g-4");
          items.forEach((item) => {
               item.classList.remove("col-12");
               item.classList.add("col-lg-4", "col-md-6");
          });

          listBtn.classList.remove("active");
          gridBtn.classList.add("active");
     }
}

// 👉 Submit Search giữ các filter
function searchSubmit() {
     const keyword = document.getElementById("searchInput").value;
     const cate = document.getElementById("cateHold")?.value;
     const minP = document.getElementById("minHold")?.value;
     const maxP = document.getElementById("maxHold")?.value;
     const brand = document.getElementById("brandHold")?.value;
     const sort = document.getElementById("sortHold")?.value;

     let url = `/products?keyword=${keyword}`;
     if (cate) url += `&category=${cate}`;
     if (minP) url += `&minPrice=${minP}`;
     if (maxP) url += `&maxPrice=${maxP}`;
     if (brand) url += `&brand=${brand}`;
     if (sort) url += `&sort=${sort}`;

     window.location.href = url;
}

// 👉 Realtime suggestions
const input = document.getElementById("searchInput");
const box = document.getElementById("suggestBox");
let timer;

input?.addEventListener("keyup", function () {
     clearTimeout(timer);
     const keyword = this.value.trim();

     if (!keyword) {
          box.style.display = "none";
          return;
     }

     timer = setTimeout(() => {
          fetch(`/api/search?keyword=${keyword}`)
               .then((res) => res.json())
               .then((data) => {
                    box.innerHTML = "";
                    if (data.length === 0) {
                         box.style.display = "none";
                         return;
                    }

                    data.forEach((name) => {
                         const a = document.createElement("a");
                         a.classList.add("list-group-item", "list-group-item-action");
                         a.textContent = name;
                         a.href = `/products?keyword=${name}`;
                         box.appendChild(a);
                    });

                    box.style.display = "block";
               });
     }, 300);
});

document.addEventListener("click", (e) => {
     if (!input.contains(e.target)) box.style.display = "none";
});
