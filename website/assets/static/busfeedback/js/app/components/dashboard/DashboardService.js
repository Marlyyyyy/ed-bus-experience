
(function () {
    'use strict';

    angular
        .module('busfeedback.dashboard.services')
        .factory('Dashboard', Dashboard);

    Dashboard.$inject = ['$http'];

    function Dashboard($http) {

        return {
            getSeatYesAndNoStatistics: getSeatYesAndNoStatistics,
            getGreetYesAndNoStatistics: getGreetYesAndNoStatistics
        };

        // Get the distribution of seat values broken down to each day of the past month
        function getSeatYesAndNoStatistics() {

            return $http.get('/bus/api/yes_and_no_statistics/', {params: {'group_by_value': 'seat'}});
        }

        // Get the distribution of greet values broken down to each day of the past month
        function getGreetYesAndNoStatistics() {

            return $http.get('/bus/api/yes_and_no_statistics/', {params: {'group_by_value': 'greet'}});
        }
    }
})();