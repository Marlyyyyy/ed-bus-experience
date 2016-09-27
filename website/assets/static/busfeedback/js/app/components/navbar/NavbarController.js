
(function () {
    'use strict';

    angular
        .module('busfeedback.navbar.controllers')
        .controller('NavbarController', NavbarController);

    NavbarController.$inject = ['$scope', 'Authentication', '$location'];

    function NavbarController($scope, Authentication, $location) {
        var vm = this;

        vm.user = Authentication.getAuthenticatedAccount();
        vm.isAuthenticated = Authentication.isAuthenticated();
        vm.logout = logout;
        vm.isActive = isActive;

        function logout() {
            Authentication.logout();
        }

        function isActive(viewLocation) {
            return viewLocation === $location.path();
        }
    }
})();