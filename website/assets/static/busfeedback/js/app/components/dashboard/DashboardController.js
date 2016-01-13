
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

        $scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
        $scope.series = ['Series A', 'Series B'];
        $scope.data = [
            [65, 59, 80, 81, 56, 55, 40],
            [28, 48, 40, 19, 86, 27, 90]
        ];
        $scope.onClick = function (points, evt) {
            console.log(points, evt);
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