/**
 * Created by Marci on 25/08/2015.
 */


(function () {
    'use strict';

    angular
        .module('runnerapp.routes')
        .config(config);

    config.$inject = ['$routeProvider'];

    /**
    * @name config
    * @desc Define valid application routes
    */
    function config($routeProvider) {
        $routeProvider.when('/register', {
            controller: 'RegisterController',
            controllerAs: 'vm',
            templateUrl: '/static/busfeedback/js/app/components/authentication/register.html',
            activeTab: 'register'
        }).when('/login', {
            controller: 'LoginController',
            controllerAs: 'vm',
            templateUrl: '/static/busfeedback/js/app/components/authentication/login.html',
            activeTab: 'login'
        }).when('/home', {
            controller: 'IndexController',
            controllerAs: 'vm',
            templateUrl: '/static/busfeedback/js/app/components/home/home.html',
            activeTab: 'home'
        }).when('/user/+:username', {
            controller: 'ProfileController',
            controllerAs: 'vm',
            templateUrl: '/static/busfeedback/js/app/components/profiles/profile.html',
            activeTab: 'profile'
        }).when('/settings', {
          controller: 'ProfileSettingsController',
          controllerAs: 'vm',
          templateUrl: '/static/busfeedback/js/app/components/profiles/settings.html',
            activeTab: 'settings'
        }).otherwise('/');
    }
})();