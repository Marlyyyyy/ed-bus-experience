
(function () {
    'use strict';

    angular
        .module('busfeedback.navbar.directives')
        .directive('navbar', navbar);

    function navbar() {

        return {
            controller: 'NavbarController',
            controllerAs: 'vm',
            restrict: 'E',
            scope: {
                posts: '='
            },
            templateUrl: '/static/busfeedback/js/app/components/navbar/navbar.html'
        };
    }
})();