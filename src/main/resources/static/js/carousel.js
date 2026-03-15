document.addEventListener('DOMContentLoaded', () => {
    const carousels = document.querySelectorAll('.item-carousel-section');

    carousels.forEach(carousel => {
        const mainImage = carousel.querySelector('#carouselMainImage');
        if (!mainImage) return;

        const thumbnails = carousel.querySelectorAll('.carousel-thumbnail');
        const prevBtn = carousel.querySelector('.carousel-control.prev');
        const nextBtn = carousel.querySelector('.carousel-control.next');
        
        // Ensure buttons exist before attaching logic
        if (!thumbnails.length || !prevBtn || !nextBtn) return;

        let currentIndex = 0;
        const imagesList = Array.from(thumbnails).map(thumb => thumb.src);

        function updateCarousel(index) {
            // Fade out
            mainImage.style.opacity = '0.5';
            
            setTimeout(() => {
                // Update source
                mainImage.src = imagesList[index];
                
                // Update thumbnails active state
                thumbnails.forEach((t, i) => {
                    if (i === index) {
                        t.classList.add('active');
                    } else {
                        t.classList.remove('active');
                    }
                });
                
                // Fade in
                mainImage.style.opacity = '1';
                currentIndex = index;
            }, 150);
        }

        thumbnails.forEach((thumb, index) => {
            thumb.addEventListener('click', () => {
                if (currentIndex !== index) {
                    updateCarousel(index);
                }
            });
        });

        prevBtn.addEventListener('click', () => {
            let newIndex = currentIndex - 1;
            if (newIndex < 0) newIndex = imagesList.length - 1;
            updateCarousel(newIndex);
        });

        nextBtn.addEventListener('click', () => {
            let newIndex = currentIndex + 1;
            if (newIndex >= imagesList.length) newIndex = 0;
            updateCarousel(newIndex);
        });
    });
});
