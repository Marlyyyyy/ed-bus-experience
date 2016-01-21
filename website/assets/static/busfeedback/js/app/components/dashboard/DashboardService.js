
(function () {
    'use strict';

    angular
        .module('busfeedback.dashboard.services')
        .factory('Dashboard', Dashboard);

    Dashboard.$inject = ['$http'];

    function Dashboard($http) {

        return {
            getSeatYesAndNoStatistics: getRideStatistics
        };

        // Get the distribution of seat values broken down to each day of the past month
        function getRideStatistics() {

            return $http.get('/bus/api/seat_yes_and_no_statistics/');
        }
    }
})();