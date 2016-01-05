
(function () {
    'use strict';

    angular
        .module('runnerapp.profiles.controllers')
        .controller('ProfileController', ProfileController);

    ProfileController.$inject = ['$location', '$routeParams', 'Profile', 'Snackbar'];

    function ProfileController($location, $routeParams, Profile, Snackbar) {

        var vm = this;
        vm.profile = undefined;

        activate();

        function activate() {
            var username = $routeParams.username;

            Profile.get(username).then(profileSuccessFn, profileErrorFn);

            function profileSuccessFn(data, status, headers, config) {
                vm.profile = data.data;
            }

            function profileErrorFn(data, status, headers, config) {
                $location.url('/');
                Snackbar.error('That user does not exist.');
            }
        }
    }
})();