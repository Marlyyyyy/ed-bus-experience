
(function () {
    'use strict';

    angular
        .module('runnerapp.home.services')
        .factory('Home', Home);

    Home.$inject = ['$http'];

    function Home($http) {

        return {
            getStatistics: getStatistics,
            getTimelineStatistics: getTimelineStatistics
        };

        // Get general statistics for any combination of arguments
        function getStatistics(service_id, start_stop_id, end_stop_id, created_at_lte, created_at_gt) {

            var params = [];
            if (typeof(service_id)!=='undefined') params.push({key: "service_id", value: service_id});
            if (typeof(start_stop_id)!=='undefined') params.push({key: "start_stop_id", value: start_stop_id});
            if (typeof(end_stop_id)!=='undefined') params.push({key: "end_stop_id", value: end_stop_id});
            if (typeof(created_at_lte)!=='undefined') params.push({key: "created_at_lte", value: created_at_lte});
            if (typeof(created_at_gt)!=='undefined') params.push({key: "created_at_gt", value: created_at_gt});

            return $http.get('/bus/api/bus_statistics/', {params: params});
        }

        function getTimelineStatistics() {
            return $http.get('/bus/api/timeline_statistics/');
        }
    }
})();