
(function () {
    'use strict';

    angular
        .module('busfeedback.dashboard.controllers')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', 'Authentication', 'Snackbar', 'Dashboard'];

    function DashboardController($scope, Authentication, Snackbar, Dashboard) {

        var vm = this;

        activate();

        function activate() {

            // Tab management
            vm.tab = 1;
            vm.selectTab = function (setTab){
                this.tab = setTab;
            };
            vm.isSelected = function(checkTab) {
                return this.tab === checkTab;
            };

            // Display statistics
            Dashboard.getSeatYesAndNoStatistics().then(seatStatisticsSuccessFn, seatStatisticsErrorFn);

            function seatStatisticsSuccessFn(data, status, headers, config) {
                console.log(data.data);

                var seatDataYes = [];
                var seatDataNo = [];
                var dayFlag = true;
                vm.seatLabels = [];
                vm.seatData = [];
                data.data.forEach(function(entry) {

                    if (entry['seat']){
                        seatDataYes.push(entry['available']);
                    }else{
                        seatDataNo.push(entry['available']);
                    }

                    if (dayFlag){
                        vm.seatLabels.push(entry['day']);
                    }

                    dayFlag = !dayFlag;
                });

                vm.seatSeries = ['Yes', 'No'];
                vm.seatData.push(seatDataYes);
                vm.seatData.push(seatDataNo);
                vm.seatOnClick = function (points, evt) {
                    console.log(points, evt);
                };
            }

            function seatStatisticsErrorFn(data, status, headers, config) {
                Snackbar.error(data.error);
            }
        }
    }
})();