
(function () {
    'use strict';

    angular
        .module('runnerapp.home.controllers')
        .controller('IndexController', IndexController);

    IndexController.$inject = ['$location', 'Authentication', 'Snackbar', 'Home'];

    function IndexController($location, Authentication, Snackbar, Home) {
        var vm = this;

        activate();

        function activate() {

            vm.isAuthenticated = Authentication.isAuthenticated();
            var authenticatedAccount = Authentication.getAuthenticatedAccount();

            // Redirect if not logged in
            if (!authenticatedAccount) {
                $location.url('/login');
                Snackbar.error('You are not authorized to view this page.');
                return;
            }

            Home.getStatistics().then(statisticsSuccessful, statisticsError);

            function statisticsSuccessful(data, status, headers, config){
                vm.labels = ["Yes", "No"];
                var seatData = [];
                seatData.push(data.data.ride_seat_positives);
                seatData.push(data.data.ride_seat_negatives);
                vm.data = seatData;
                vm.type = 'Pie';

                vm.toggle = function () {
                    vm.type = vm.type === 'PolarArea' ?
                        'Pie' : 'PolarArea';
                };
            }

            function statisticsError(data, status, headers, config) {
                Snackbar.error('Statistics could not be retrieved.');
            }
        }
    }
})();