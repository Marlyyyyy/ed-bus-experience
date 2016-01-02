/**
 * Created by Marci on 27/08/2015.
 */


/**
* NavbarController
* @namespace runnerapp.controllers
*/
(function () {
    'use strict';

    angular
        .module('runnerapp.navbar.controllers')
        .controller('NavbarController', NavbarController);

    NavbarController.$inject = ['$scope', 'Authentication', '$location'];

    /**
    * @namespace NavbarController
    */
    function NavbarController($scope, Authentication, $location) {
        var vm = this;

        vm.user = Authentication.getAuthenticatedAccount();
        vm.isAuthenticated = Authentication.isAuthenticated();
        vm.logout = logout;
        vm.isActive = isActive;

        /**
        * @name logout
        * @desc Log the user out
        * @memberOf runnerapp.navbar.controllers.NavbarController
        */
        function logout() {
            Authentication.logout();
        }

        function isActive(viewLocation) {
            return viewLocation === $location.path();
        }
    }
})();