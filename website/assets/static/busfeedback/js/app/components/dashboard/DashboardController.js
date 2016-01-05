
(function () {
    'use strict';

    angular
        .module('busfeedback.dashboard.controllers')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', 'Authentication', 'Snackbar'];

    function DashboardController($scope, Authentication, Snackbar) {

        var vm = this;
        this.tab = 1;
        this.selectTab = function (setTab){
            this.tab = setTab;
        };
        this.isSelected = function(checkTab) {
            return this.tab === checkTab;
        };

        function activate() {

            function logsSuccessFn(data, status, headers, config) {
                vm.logs = data.data;
            }

            function logsErrorFn(data, status, headers, config) {
                Snackbar.error(data.error);
            }
        }
    }
})();