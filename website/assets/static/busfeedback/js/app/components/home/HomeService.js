
(function () {
    'use strict';

    angular
        .module('runnerapp.home.services')
        .factory('Home', Home);

    Home.$inject = ['$http'];

    function Home($http) {

        return {
            getRideStatistics: getRideStatistics,
            getTimelineStatistics: getTimelineStatistics
        };

        // Get general statistics for any combination of arguments
        function getRideStatistics(queryParameters) {
            console.log(queryParameters);
            var params = [];
            if (typeof(queryParameters.service_id)!=='undefined') params.push({key: "service_id", value: queryParameters.service_id});
            if (typeof(queryParameters.start_stop_id)!=='undefined') params.push({key: "start_stop_id", value: queryParameters.start_stop_id});
            if (typeof(queryParameters.end_stop_id)!=='undefined') params.push({key: "end_stop_id", value: queryParameters.end_stop_id});
            if (typeof(queryParameters.created_at_lte)!=='undefined') params.push({key: "created_at_lte", value: queryParameters.created_at_lte});
            if (typeof(queryParameters.created_at_gt)!=='undefined') params.push({key: "created_at_gt", value: queryParameters.created_at_gt});

            return $http.get('/bus/api/bus_statistics/', {params: params});
        }

        function getTimelineStatistics() {
            return $http.get('/bus/api/timeline_statistics/');
        }
    }
})();