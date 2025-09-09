// Save JWT in localStorage on page load
   document.addEventListener("DOMContentLoaded", function () {
       let token = document.getElementById("jwtToken").textContent;
       if (token && token.trim() !== "") {
           localStorage.setItem("jwt", token);
       }
   });

   // Helper function to get JWT and redirect
   function navigateWithToken(url) {
       let token = localStorage.getItem('jwt');
       if (token) {
           window.location.href = url + '?jwt_token=' + encodeURIComponent(token);
       } else {
           alert('Your session has expired. Please log in again.');
           window.location.href = '/api/auth/login';
       }
   }

   function navigateToProfile() {
       navigateWithToken('/api/auth/update');
   }

   function goToManageUsers() {
       navigateWithToken('/api/auth/getallusers');
   }

   function logout() {
       localStorage.removeItem('jwt');
       window.location.href = '/api/auth/login';
   }