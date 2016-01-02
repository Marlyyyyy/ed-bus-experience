/**
 * Created by Marci on 28/08/2015.
 */

/**
* ProfileController
* @namespace runnerapp.profiles.controllers
*/
(function () {
    'use strict';

    angular
        .module('runnerapp.profiles.controllers')
        .controller('ProfileController', ProfileController);

    ProfileController.$inject = ['$location', '$routeParams', 'Profile', 'Snackbar'];

    /**
    * @namespace ProfileController
    */
    function ProfileController($location, $routeParams, Profile, Snackbar) {
        var vm = this;

        vm.profile = undefined;
        vm.posts = [];
        vm.logs = [];

        activate();

        /**
        * @name activate
        * @desc Actions to be performed when this controller is instantiated
        * @memberOf runnerapp.profiles.controllers.ProfileController
        */
        function activate() {
            var username = $routeParams.username;

            Profile.get(username).then(profileSuccessFn, profileErrorFn);

            /**
            * @name profileSuccessProfile
            * @desc Update `profile` on viewmodel
            */
            function profileSuccessFn(data, status, headers, config) {
                vm.profile = data.data;
            }

            /**
            * @name profileErrorFn
            * @desc Redirect to index and show error Snackbar
            */
            function profileErrorFn(data, status, headers, config) {
                $location.url('/');
                Snackbar.error('That user does not exist.');
            }

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