document.addEventListener('DOMContentLoaded', () => {
    // Hidden inputs
    const fileInput = document.getElementById('images');
    const mainImageIdInput = document.getElementById('mainImageId');
    const newMainImageIndexInput = document.getElementById('newMainImageIndex');
    const deletedImagesContainer = document.getElementById('deletedImagesContainer');
    
    if (!fileInput) return;

    // UI elements
    const uploadPlaceholder = document.getElementById('uploadPlaceholder');
    const formCarouselArea = document.getElementById('formCarouselArea');
    const mainImgNode = document.getElementById('formMainImg');
    const thumbsContainer = document.getElementById('formThumbnails');
    const btnSetMain = document.getElementById('btnSetMain');
    const btnDelete = document.getElementById('btnDelete');
    
    const prevBtn = document.querySelector('.form-carousel-control.prev');
    const nextBtn = document.querySelector('.form-carousel-control.next');

    // State
    // existingImages: array of { id: string, url: string, isDeleted: boolean }
    let existingImages = [];
    // currentNewFiles: array of File objects
    let currentNewFiles = [];      
    let newObjectUrls = [];        

    // Current focused view
    let focusedType = 'existing'; // 'existing' or 'new'
    let focusedIndex = 0;         // index within the respective array

    // Main image tracking
    let mainType = 'existing'; // 'existing' or 'new'
    let mainRefId = null;      // ID if existing
    let mainRefIndex = null;   // Index if new

    // 1. Parse initial existing images from DOM
    const existingDataNodes = document.querySelectorAll('.existing-image-data');
    existingDataNodes.forEach((node, index) => {
        const id = node.dataset.id;
        const url = node.dataset.url;
        const isMain = node.dataset.main === 'true';
        
        existingImages.push({ id, url, isDeleted: false });
        
        if (isMain || (mainRefId === null && index === 0)) {
            mainType = 'existing';
            mainRefId = id;
            mainRefIndex = null;
        }
    });

    // If there were no existing images (edge case), setup accordingly
    if (existingImages.length === 0) {
        focusedType = 'new';
        mainType = 'new';
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

    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length) {
            handleFiles(Array.from(e.target.files));
        }
    });

    function getActiveExisting() {
        return existingImages.map((img, idx) => ({ ...img, originalIndex: idx })).filter(img => !img.isDeleted);
    }

    function getTotalActiveCount() {
        return getActiveExisting().length + currentNewFiles.length;
    }

    function syncHiddenInputs() {
        // Sync new files to DataTransfer
        const dt = new DataTransfer();
        currentNewFiles.forEach(file => dt.items.add(file));
        fileInput.files = dt.files;

        // Sync main image inputs
        if (getTotalActiveCount() === 0) {
            mainImageIdInput.value = '';
            newMainImageIndexInput.value = '';
        } else if (mainType === 'existing') {
            mainImageIdInput.value = mainRefId;
            newMainImageIndexInput.value = '';
        } else if (mainType === 'new') {
            mainImageIdInput.value = '';
            newMainImageIndexInput.value = mainRefIndex;
        }

        // Sync deleted existing images
        deletedImagesContainer.innerHTML = '';
        existingImages.forEach(img => {
            if (img.isDeleted) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'deletedImageIds';
                input.value = img.id;
                deletedImagesContainer.appendChild(input);
            }
        });
    }

    function autoAssignMainImage() {
        if (getTotalActiveCount() === 0) {
            mainType = null;
            mainRefId = null;
            mainRefIndex = null;
            return;
        }

        // Prefer first active existing image
        const activeExisting = getActiveExisting();
        if (activeExisting.length > 0) {
            mainType = 'existing';
            mainRefId = activeExisting[0].id;
            mainRefIndex = null;
        } else if (currentNewFiles.length > 0) {
            mainType = 'new';
            mainRefId = null;
            mainRefIndex = 0;
        }
    }

    function findFirstAvailableFocus() {
        const activeExisting = getActiveExisting();
        if (activeExisting.length > 0) {
            focusedType = 'existing';
            focusedIndex = activeExisting[0].originalIndex;
        } else if (currentNewFiles.length > 0) {
            focusedType = 'new';
            focusedIndex = 0;
        }
    }

    function handleFiles(newFiles) {
        const imageFiles = newFiles.filter(file => file.type.startsWith('image/'));
        if (!imageFiles.length) return;

        imageFiles.forEach(file => {
            currentNewFiles.push(file);
            newObjectUrls.push(URL.createObjectURL(file));
        });

        // If this is the very first image overall, auto-focus
        const totalActive = getTotalActiveCount();
        if (totalActive === imageFiles.length) { // Means it was 0 before
            focusedType = 'new';
            focusedIndex = 0;
            mainType = 'new';
            mainRefIndex = 0;
        }

        renderCarousel();
    }

    function renderCarousel() {
        const activeExisting = getActiveExisting();
        const totalActiveCount = getTotalActiveCount();

        if (totalActiveCount === 0) {
            uploadPlaceholder.style.display = 'flex';
            formCarouselArea.classList.remove('active');
            syncHiddenInputs();
            return;
        } else {
            uploadPlaceholder.style.display = 'none';
            formCarouselArea.classList.add('active');
        }
        
        // Ensure focused item is valid, otherwise look for fallback
        if (focusedType === 'existing') {
            if (existingImages[focusedIndex] == null || existingImages[focusedIndex].isDeleted) {
                findFirstAvailableFocus();
            }
        } else if (focusedType === 'new') {
            if (focusedIndex >= currentNewFiles.length) {
                findFirstAvailableFocus();
            }
        }

        syncHiddenInputs();

        // 1. Update Main Image View
        mainImgNode.style.opacity = '0.5';
        setTimeout(() => {
            if (focusedType === 'existing') {
                mainImgNode.src = existingImages[focusedIndex].url;
            } else {
                mainImgNode.src = newObjectUrls[focusedIndex];
            }
            mainImgNode.style.opacity = '1';
        }, 150);

        // 2. Update Overlay Buttons
        let isCurrentlyMain = false;
        if (focusedType === 'existing') {
            isCurrentlyMain = (mainType === 'existing' && existingImages[focusedIndex].id === mainRefId);
        } else {
            isCurrentlyMain = (mainType === 'new' && focusedIndex === mainRefIndex);
        }

        if (isCurrentlyMain) {
            btnSetMain.textContent = 'Головне фото';
            btnSetMain.classList.add('is-main');
        } else {
            btnSetMain.textContent = 'Встановити головною';
            btnSetMain.classList.remove('is-main');
        }

        // 3. Update Thumbnails
        thumbsContainer.innerHTML = ''; 
        
        // Render Existing Thumbs
        activeExisting.forEach(img => {
            const wrapper = document.createElement('div');
            wrapper.className = 'form-thumbnail-wrapper';
            
            const isFoc = (focusedType === 'existing' && focusedIndex === img.originalIndex);
            
            const imgNode = document.createElement('img');
            imgNode.src = img.url;
            imgNode.className = 'form-thumbnail' + (isFoc ? ' active' : '');
            
            imgNode.addEventListener('click', () => {
                if (!isFoc) {
                    focusedType = 'existing';
                    focusedIndex = img.originalIndex;
                    renderCarousel();
                }
            });

            wrapper.appendChild(imgNode);
            thumbsContainer.appendChild(wrapper);
        });

        // Render New Thumbs
        newObjectUrls.forEach((url, i) => {
            const wrapper = document.createElement('div');
            wrapper.className = 'form-thumbnail-wrapper';
            
            const isFoc = (focusedType === 'new' && focusedIndex === i);

            const imgNode = document.createElement('img');
            imgNode.src = url;
            imgNode.className = 'form-thumbnail new-upload-thumb' + (isFoc ? ' active' : '');
            
            // tiny badge to indicate this is a new image
            const badge = document.createElement('div');
            badge.style.position = 'absolute';
            badge.style.top = '2px';
            badge.style.right = '2px';
            badge.style.width = '12px';
            badge.style.height = '12px';
            badge.style.backgroundColor = 'var(--caramel)';
            badge.style.borderRadius = '50%';
            badge.title = 'Нове завантаження';
            
            imgNode.addEventListener('click', () => {
                if (!isFoc) {
                    focusedType = 'new';
                    focusedIndex = i;
                    renderCarousel();
                }
            });

            wrapper.appendChild(imgNode);
            wrapper.appendChild(badge);
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
        if (totalActiveCount > 1) {
            prevBtn.style.display = 'flex';
            nextBtn.style.display = 'flex';
        } else {
            prevBtn.style.display = 'none';
            nextBtn.style.display = 'none';
        }
    }

    // Next/Prev Math
    function getLinearSequence() {
        const seq = [];
        getActiveExisting().forEach(e => seq.push({ type: 'existing', idx: e.originalIndex }));
        newObjectUrls.forEach((_, i) => seq.push({ type: 'new', idx: i }));
        return seq;
    }

    prevBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        const seq = getLinearSequence();
        const currSeqIdx = seq.findIndex(s => s.type === focusedType && s.idx === focusedIndex);
        let nextSeqIdx = currSeqIdx - 1;
        if (nextSeqIdx < 0) nextSeqIdx = seq.length - 1;
        
        focusedType = seq[nextSeqIdx].type;
        focusedIndex = seq[nextSeqIdx].idx;
        renderCarousel();
    });

    nextBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        const seq = getLinearSequence();
        const currSeqIdx = seq.findIndex(s => s.type === focusedType && s.idx === focusedIndex);
        let nextSeqIdx = currSeqIdx + 1;
        if (nextSeqIdx >= seq.length) nextSeqIdx = 0;
        
        focusedType = seq[nextSeqIdx].type;
        focusedIndex = seq[nextSeqIdx].idx;
        renderCarousel();
    });

    // Action Overlays
    btnSetMain?.addEventListener('click', (e) => {
        e.preventDefault();
        if (btnSetMain.classList.contains('is-main')) return;

        if (focusedType === 'existing') {
            mainType = 'existing';
            mainRefId = existingImages[focusedIndex].id;
            mainRefIndex = null;
        } else {
            mainType = 'new';
            mainRefId = null;
            mainRefIndex = focusedIndex;
        }
        renderCarousel();
    });

    btnDelete?.addEventListener('click', (e) => {
        e.preventDefault();
        
        let wasMain = false;

        if (focusedType === 'existing') {
            wasMain = (mainType === 'existing' && mainRefId === existingImages[focusedIndex].id);
            existingImages[focusedIndex].isDeleted = true;
        } else {
            wasMain = (mainType === 'new' && mainRefIndex === focusedIndex);
            
            // Remove from currentNewFiles
            currentNewFiles.splice(focusedIndex, 1);
            URL.revokeObjectURL(newObjectUrls[focusedIndex]);
            newObjectUrls.splice(focusedIndex, 1);

            // Shift newMainImageIndex left if necessary
            if (mainType === 'new' && mainRefIndex > focusedIndex) {
                mainRefIndex--;
            }
        }

        if (wasMain) {
            autoAssignMainImage();
        }

        renderCarousel();
    });

    // Initial render
    renderCarousel();
});
