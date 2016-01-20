
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
            Home.getTimelineStatistics().then(timelineStatisticsSuccessful, timelineStatisticsError);

            function statisticsSuccessful(data, status, headers, config){

                console.log(data.data);

                // Seat Pie
                vm.seatPieLabels = ["Yes", "No"];
                var seatData = [];
                seatData.push(data.data.seat_positives);
                seatData.push(data.data.seat_negatives);
                vm.seatPieData = seatData;
                vm.seatPieType = 'Pie';

                // Greet Pie
                vm.greetPieLabels = ["Yes", "No"];
                var greetData = [];
                greetData.push(data.data.greet_positives);
                greetData.push(data.data.greet_negatives);
                vm.greetPieData = greetData;
                vm.greetPieType = 'Pie';

                // General
                vm.numberOfJourneys = data.data.number_of_journeys;
                vm.numberOfRides = data.data.number_of_rides;
                vm.averageRating = data.data.average_rating;
            }

            function statisticsError(data, status, headers, config) {
                Snackbar.error('Statistics could not be retrieved.');
            }

            function timelineStatisticsSuccessful(data, status, headers, config){
                var timelineLabels = [];
                var timelineData = [];

                data.data.forEach(function(entry) {
                    timelineLabels.push(entry['day']);
                    timelineData.push(entry['available']);
                });

                vm.timelineLabels = timelineLabels;
                vm.timelineSeries = ['Number of Rides'];

                vm.timelineData = [timelineData];
            }

            function timelineStatisticsError(data, status, headers, config) {
                Snackbar.error('Timeline statistics could not be retrieved.');
            }
        }
    }
})();