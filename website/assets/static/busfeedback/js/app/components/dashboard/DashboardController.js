
(function () {
    'use strict';

    angular
        .module('busfeedback.dashboard.controllers')
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', 'Authentication', 'Snackbar', 'Dashboard', 'uiGmapGoogleMapApi', 'uiGmapIsReady'];

    function DashboardController($scope, Authentication, Snackbar, Dashboard, uiGmapGoogleMapApi, uiGmapIsReady) {

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
            vm.chartOptions = {
                animation: false
            };

            Dashboard.getSeatYesAndNoStatistics().then(seatStatisticsSuccessFn, seatStatisticsErrorFn);
            Dashboard.getGreetYesAndNoStatistics().then(greetStatisticsSuccessFn, greetStatisticsErrorFn);
            Dashboard.getAverageWaitDurationStatistics().then(averageWaitDurationStatisticsSuccessFn, averageWaitDurationStatisticsErrorFn);
            Dashboard.getAllRides().then(getAllRidesSuccessFn, getAllRidesErrorFn);

            // Display the map
            var center = {
                latitude: 55.9531,
                longitude: -3.1889
            };

            // Fix for grey map
            vm.control = {};

            uiGmapIsReady.promise().then(function (maps) {
                vm.control.refresh();
            });

            uiGmapGoogleMapApi.then(function(maps) {
            });

            vm.map = {
                center: center,
                zoom: 11,
                markers: [],
                events: {
                    click: function (map, eventName, originalEventArgs) {
                        var e = originalEventArgs[0];
                        var lat = e.latLng.lat(), lon = e.latLng.lng();
                        var marker = {
                            id: Date.now(),
                            coords: {
                                latitude: lat,
                                longitude: lon
                            }
                        };
                        vm.map.markers.push(marker);
                        console.log(vm.map.markers);
                        vm.$apply();
                    }
                }
            };

            function seatStatisticsSuccessFn(data, status, headers, config) {

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

            function greetStatisticsSuccessFn(data, status, headers, config) {

                var greetDataYes = [];
                var greetDataNo = [];
                var dayFlag = true;
                vm.greetLabels = [];
                vm.greetData = [];
                data.data.forEach(function(entry) {

                    if (entry['greet']){
                        greetDataYes.push(entry['available']);
                    }else{
                        greetDataNo.push(entry['available']);
                    }

                    if (dayFlag){
                        vm.greetLabels.push(entry['day']);
                    }

                    dayFlag = !dayFlag;
                });

                vm.greetSeries = ['Yes', 'No'];
                vm.greetData.push(greetDataYes);
                vm.greetData.push(greetDataNo);
                vm.greetOnClick = function (points, evt) {
                    console.log(points, evt);
                };
            }

            function greetStatisticsErrorFn(data, status, headers, config) {
                Snackbar.error(data.error);
            }

            function averageWaitDurationStatisticsSuccessFn(data, status, headers, config) {

                var averageData = [];
                vm.averageWaitDurationLabels = [];
                vm.averageWaitDurationData = [];
                data.data.forEach(function(entry) {
                    averageData.push(entry['average'] / 1000);
                    vm.averageWaitDurationLabels.push(entry['day']);
                });

                vm.averageWaitDurationSeries = ['Wait Duration in seconds'];
                vm.averageWaitDurationData.push(averageData);
                vm.averageWaitDurationOnClick = function (points, evt) {
                    console.log(points, evt);
                };
            }

            function averageWaitDurationStatisticsErrorFn(data, status, headers, config) {
                Snackbar.error(data.error);
            }

            function getAllRidesSuccessFn(data, status, headers, config) {

                var rides = data.data.rides;
                var marker;
                var ride;

                for (var i = 0; i < rides.length; i++) {
                    ride = rides[i];

                    marker = {
                        id: "marker" + i,
                        coords: {
                            latitude: ride["start_stop"]["latitude"],
                            longitude: ride["start_stop"]["longitude"]
                        }
                    };

                    vm.map.markers.push(marker);
                }
            }

            function getAllRidesErrorFn(data, status, headers, config) {
                Snackbar.error(data.error);
            }
        }
    }
})();