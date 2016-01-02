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
            'runnerapp.profiles'
    ]);

    angular
        .module('runnerapp.routes', ['ngRoute']);

    angular
        .module('config', []);

    angular
        .module('runnerapp')
        .run(run);

    run.$inject = ['$http'];

    /**
    * @name run
    * @desc Update xsrf $http headers to align with Django's defaults
    */
    function run($http) {
      $http.defaults.xsrfHeaderName = 'X-CSRFToken';
      $http.defaults.xsrfCookieName = 'csrftoken';
    }
})();