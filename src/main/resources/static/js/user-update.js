document.addEventListener('DOMContentLoaded', function() {
    const updateUserForm = document.getElementById('updateUserForm');
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    const passwordMatchError = document.getElementById('passwordMatchError');

    // On form submit, check if passwords match
    if (updateUserForm) {
        updateUserForm.addEventListener('submit', function(event) {
            // Only validate matching if the user is trying to change password
            if (newPassword.value !== '' || confirmPassword.value !== '') {
                if (newPassword.value !== confirmPassword.value) {
                    event.preventDefault(); // Prevent form submission
                    passwordMatchError.style.display = 'block';
                    confirmPassword.style.borderColor = '#dc3545'; // Highlight error
                    
                    // Optionally scroll to error
                    confirmPassword.scrollIntoView({ behavior: 'smooth', block: 'center' });
                } else {
                    passwordMatchError.style.display = 'none';
                    confirmPassword.style.borderColor = ''; // Reset
                }
            }
        });
    }

    // Dynamic error hiding while typing
    const hideErrorOnChange = function() {
        if (passwordMatchError.style.display === 'block') {
            if (newPassword.value === confirmPassword.value) {
                passwordMatchError.style.display = 'none';
                confirmPassword.style.borderColor = '';
            }
        }
    };

    if (newPassword) newPassword.addEventListener('input', hideErrorOnChange);
    if (confirmPassword) confirmPassword.addEventListener('input', hideErrorOnChange);
});
