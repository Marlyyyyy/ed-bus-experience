angular
    .module('runnerapp', []);

(function () {
    'use strict';

    angular
        .module('runnerapp', [
            'runnerapp.routes',
            'config',
            'authentication',
            'runnerapp.navbar',
            'runnerapp.home',
            'runnerapp.snackbar',
            'runnerapp.profiles',
            'busfeedback.dashboard',
            'uiGmapgoogle-maps'
        ]);

    angular
        .module('runnerapp.routes', ['ngRoute']);

    angular
        .module('config', []);

    angular
        .module('runnerapp')
        .factory('httpRequestInterceptor', httpRequestInterceptor)
        .config(function($httpProvider, $mdThemingProvider, uiGmapGoogleMapApiProvider) {
            $httpProvider.interceptors.push('httpRequestInterceptor');

            $mdThemingProvider.theme('default')
                .primaryPalette('blue')
                .accentPalette('blue');

            uiGmapGoogleMapApiProvider.configure({
                //    key: 'your api key',
                v: '3.20', //defaults to latest 3.X anyhow
                libraries: 'weather,geometry,visualization'
            });
        })
        .run(run);

    run.$inject = ['$http'];

    function run($http) {
        $http.defaults.xsrfHeaderName = 'X-CSRFToken';
        $http.defaults.xsrfCookieName = 'csrftoken';
    }

    function httpRequestInterceptor($cookies) {
        return {
            request: function (config) {

                var token = $cookies.get('token');
                if (typeof token !== "undefined"){
                    config.headers['Authorization'] = 'JWT ' + token;
                }

                return config;
            },

            requestError: function(config) {
                return config;
            },

            response: function(res) {
                return res;
            },

            responseError: function(res) {
                return res;
            }
        }
    }
})();