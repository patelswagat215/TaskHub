// Function to submit form with JWT token as URL parameter
function submitForm() {
    let token = localStorage.getItem('jwt');
    if (!token) {
        alert('No JWT token found. Please login again.');
        window.location.href = '/api/auth/login';
        return;
    }

    let form = document.getElementById('profileForm');
    let formData = new FormData(form);
    
    // Add JWT token to form data
    formData.append('jwt_token', token);

    fetch('/api/auth/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams(formData)
    })
    .then(response => response.text())   // ✅ Get Thymeleaf HTML back
    .then(html => {
        // ✅ Replace current page with updated Thymeleaf-rendered HTML
        document.open();
        document.write(html);
        document.close();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Update failed. Please try again.');
    });
}

// Function to go back to welcome page
function goBack() {
    let token = localStorage.getItem('jwt');
    if (token) {
        window.location.href = '/api/auth/home?jwt_token=' + encodeURIComponent(token);
    } else {
        window.location.href = '/api/auth/login';
    }
}

// Logout function
function logout() {
    localStorage.removeItem('jwt');
    window.location.href = '/api/auth/login';
}

// Check if user is authenticated
document.addEventListener('DOMContentLoaded', function() {
    let token = localStorage.getItem('jwt');
    if (!token) {
        // Try to get token from URL parameters
        const urlParams = new URLSearchParams(window.location.search);
        token = urlParams.get('jwt_token');
        if (token) {
            localStorage.setItem('jwt', token);
        } else {
            alert('Please login first!');
            window.location.href = '/api/auth/login';
        }
    }
});