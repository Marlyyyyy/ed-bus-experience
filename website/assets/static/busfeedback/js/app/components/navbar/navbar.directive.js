/**
 * Created by Marci on 01/09/2015.
 */

/**
* Posts
* @namespace runnerapp.navbar.directives
*/
(function () {
    'use strict';

    angular
        .module('runnerapp.navbar.directives')
        .directive('navbar', navbar);

    /**
    * @namespace Posts
    */
    function navbar() {
        /**
        * @name directive
        * @desc The directive to be returned
        * @memberOf runnerapp.navbar.directives.navbar
        */
        var directive = {
            controller: 'NavbarController',
            controllerAs: 'vm',
            restrict: 'E',
            scope: {
                posts: '='
            },
            templateUrl: '/static/busfeedback/js/app/components/navbar/navbar.html'
        };

        return directive;
    }
})();