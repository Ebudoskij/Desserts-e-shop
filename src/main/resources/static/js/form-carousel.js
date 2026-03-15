document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('images');
    const mainIndexInput = document.getElementById('mainImageIndex');
    if (!fileInput || !mainIndexInput) return;

    const uploadPlaceholder = document.getElementById('uploadPlaceholder');
    const formCarouselArea = document.getElementById('formCarouselArea');
    const mainImgNode = document.getElementById('formMainImg');
    const thumbsContainer = document.getElementById('formThumbnails');
    const btnSetMain = document.getElementById('btnSetMain');
    const btnDelete = document.getElementById('btnDelete');
    
    const prevBtn = document.querySelector('.form-carousel-control.prev');
    const nextBtn = document.querySelector('.form-carousel-control.next');

    let currentFiles = [];      // Array to keep track of accumulated File objects
    let objectUrls = [];        // Corresponding Object URLs for preview
    let currentIndex = 0;       // Which image is currently viewed
    let mainIndex = 0;          // Which image is designated as main

    // Initialize from existing input if any (browsers might remember on back navigation)
    if (fileInput.files.length > 0) {
        handleFiles(Array.from(fileInput.files));
    }

    // Connect placeholder click to input click
    uploadPlaceholder.addEventListener('click', () => fileInput.click());
    
    // Connect "Add More" thumb click to input click
    const initAddMoreBtn = () => {
        const addMoreBtns = document.querySelectorAll('.add-more-thumb');
        addMoreBtns.forEach(btn => {
            btn.onclick = () => fileInput.click();
        });
    }

    // Drag and Drop
    uploadPlaceholder.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadPlaceholder.classList.add('dragover');
    });
    uploadPlaceholder.addEventListener('dragleave', () => {
        uploadPlaceholder.classList.remove('dragover');
    });
    uploadPlaceholder.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadPlaceholder.classList.remove('dragover');
        if (e.dataTransfer.files.length) {
            handleFiles(Array.from(e.dataTransfer.files));
        }
    });

    // File input changes
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length) {
            handleFiles(Array.from(e.target.files));
        }
    });

    function handleFiles(newFiles) {
        // Filter only images
        const imageFiles = newFiles.filter(file => file.type.startsWith('image/'));
        if (!imageFiles.length) return;

        // Append to tracked arrays
        imageFiles.forEach(file => {
            currentFiles.push(file);
            objectUrls.push(URL.createObjectURL(file));
        });

        syncInputFiles();
        
        // If this is the first upload, show carousel and update index to 0
        if (currentFiles.length === imageFiles.length) {
            currentIndex = 0;
            mainIndex = 0;
            uploadPlaceholder.style.display = 'none';
            formCarouselArea.classList.add('active');
        } else {
            // Option to jump to the newly added image here?
            // currentIndex = currentFiles.length - imageFiles.length; // Focus on first new
        }

        renderCarousel();
    }

    function syncInputFiles() {
        const dt = new DataTransfer();
        currentFiles.forEach(file => dt.items.add(file));
        fileInput.files = dt.files;
        mainIndexInput.value = mainIndex;
    }

    function renderCarousel() {
        if (currentFiles.length === 0) {
            uploadPlaceholder.style.display = 'flex';
            formCarouselArea.classList.remove('active');
            mainIndexInput.value = 0;
            return;
        }

        // Clamp indices
        if (currentIndex >= currentFiles.length) currentIndex = currentFiles.length - 1;
        if (mainIndex >= currentFiles.length) mainIndex = 0;
        
        syncInputFiles();

        // 1. Update Main Image
        mainImgNode.style.opacity = '0.5';
        setTimeout(() => {
            mainImgNode.src = objectUrls[currentIndex];
            mainImgNode.style.opacity = '1';
        }, 150);

        // 2. Update Overlay Buttons
        if (currentIndex === mainIndex) {
            btnSetMain.textContent = 'Головне фото';
            btnSetMain.classList.add('is-main');
        } else {
            btnSetMain.textContent = 'Встановити головною';
            btnSetMain.classList.remove('is-main');
        }

        // 3. Update Thumbnails
        thumbsContainer.innerHTML = ''; // clear
        
        objectUrls.forEach((url, i) => {
            const wrapper = document.createElement('div');
            wrapper.className = 'form-thumbnail-wrapper';
            
            const img = document.createElement('img');
            img.src = url;
            img.className = 'form-thumbnail' + (i === currentIndex ? ' active' : '');
            
            img.addEventListener('click', () => {
                if (currentIndex !== i) {
                    currentIndex = i;
                    renderCarousel();
                }
            });

            wrapper.appendChild(img);
            thumbsContainer.appendChild(wrapper);
        });

        // Add the "+ Add more" box inside thumbs
        const addMore = document.createElement('div');
        addMore.className = 'add-more-thumb';
        addMore.title = "Додати ще фото";
        addMore.innerHTML = '<svg viewBox="0 0 24 24"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>';
        thumbsContainer.appendChild(addMore);
        initAddMoreBtn();
        
        // Hide/Show controls based on count
        if (currentFiles.length > 1) {
            prevBtn.style.display = 'flex';
            nextBtn.style.display = 'flex';
        } else {
            prevBtn.style.display = 'none';
            nextBtn.style.display = 'none';
        }
    }

    // Next/Prev Buttons Logic
    prevBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        currentIndex = currentIndex - 1;
        if (currentIndex < 0) currentIndex = currentFiles.length - 1;
        renderCarousel();
    });

    nextBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        currentIndex = currentIndex + 1;
        if (currentIndex >= currentFiles.length) currentIndex = 0;
        renderCarousel();
    });

    // Action Overlays
    btnSetMain?.addEventListener('click', (e) => {
        e.preventDefault();
        if (currentIndex !== mainIndex) {
            mainIndex = currentIndex;
            renderCarousel();
        }
    });

    btnDelete?.addEventListener('click', (e) => {
        e.preventDefault();
        
        // Remove from arrays
        currentFiles.splice(currentIndex, 1);
        URL.revokeObjectURL(objectUrls[currentIndex]); // free memory
        objectUrls.splice(currentIndex, 1);
        
        // Handle main index shift
        if (mainIndex === currentIndex) {
            mainIndex = 0; // reset to 0 if we deleted main
        } else if (mainIndex > currentIndex) {
            mainIndex--; // shift left
        }

        // Handle current index shift
        if (currentIndex >= currentFiles.length) {
            currentIndex = currentFiles.length - 1;
        }

        renderCarousel();
    });

});
