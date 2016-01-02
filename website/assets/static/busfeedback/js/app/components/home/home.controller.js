/**
 * Created by Marci on 28/08/2015.
 */

/**
* IndexController
* @namespace runnerapp.home.controllers
*/
(function () {
    'use strict';

    angular
        .module('runnerapp.home.controllers')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$scope', 'Authentication', 'Logs', 'Snackbar', 'ngDialog'];

    /**
    * @namespace IndexController
    */
    function IndexController($scope, Authentication, Snackbar, ngDialog) {
        var vm = this;

        vm.isAuthenticated = Authentication.isAuthenticated();
        vm.logs = [];
        vm.displayingUser = Authentication.getAuthenticatedAccount().username;

        vm.newPost = newPost;

        activate();

        function newPost(){

            ngDialog.open({
                template: '/static/runnerapp/js/app/components/logs/new-log.html',
                className: 'custom-modal-theme',
                controller: 'NewLogController as vm'
            });
        }

        /**
        * @name activate
        * @desc Actions to be performed when this controller is instantiated
        * @memberOf runnerapp.home.controllers.IndexController
        */
        function activate() {

            /**
            * @name logsSuccessFn
            * @desc Update posts array on view
            */
            function logsSuccessFn(data, status, headers, config) {
                vm.logs = data.data;
            }


            /**
            * @name logsErrorFn
            * @desc Show snackbar with error
            */
            function logsErrorFn(data, status, headers, config) {
                Snackbar.error(data.error);
            }
        }
    }
})();