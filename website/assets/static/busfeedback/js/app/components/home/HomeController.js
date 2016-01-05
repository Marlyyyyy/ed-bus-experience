
(function () {
    'use strict';

    angular
        .module('runnerapp.home.controllers')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$scope', 'Authentication', 'Snackbar'];

    function IndexController($scope, Authentication, Snackbar) {
        var vm = this;

        vm.isAuthenticated = Authentication.isAuthenticated();
        vm.logs = [];
        vm.username = Authentication.getAuthenticatedAccount();

        vm.newPost = newPost;

        activate();

        function newPost(){

        }

        function activate() {

            function logsSuccessFn(data, status, headers, config) {
                vm.logs = data.data;
            }

            function logsErrorFn(data, status, headers, config) {
                Snackbar.error(data.error);
            }
        }
    }
})();