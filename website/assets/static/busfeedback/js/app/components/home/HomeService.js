
(function () {
    'use strict';

    angular
        .module('runnerapp.home.services')
        .factory('Home', Home);

    Home.$inject = ['$http'];

    function Home($http) {

        return {
            getStatistics: getStatistics
        };

        function getStatistics() {
            return $http.get('/bus/api/general_statistics/');
        }
    }
})();