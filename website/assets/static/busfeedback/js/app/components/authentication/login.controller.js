/**
 * Created by Marci on 26/08/2015.
 */

/**
* LoginController
* @namespace authentication.controllers
*/
(function () {
    'use strict';

    angular
        .module('authentication.controllers')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$location', '$scope', 'Authentication'];

    /**
    * @namespace LoginController
    */
    function LoginController($location, $scope, Authentication) {
        var vm = this;

        vm.login = login;

        activate();

        /**
        * @name activate
        * @desc Actions to be performed when this controller is instantiated
        * @memberOf authentication.controllers.LoginController
        */
        function activate() {
            // If the user is authenticated, they should not be here.
            if (Authentication.isAuthenticated()) {
                $location.url('/home');
            }
        }

        /**
        * @name login
        * @desc Log the user in
        * @memberOf authentication.controllers.LoginController
        */
        function login() {
            Authentication.login(vm.email, vm.password);
        }
    }
})();