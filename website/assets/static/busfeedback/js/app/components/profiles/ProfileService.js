
(function () {
    'use strict';

    angular
        .module('runnerapp.profiles.services')
        .factory('Profile', Profile);

    Profile.$inject = ['$http'];

    function Profile($http) {

        return {
            destroy: destroy,
            get: get,
            update: update
        };

        function destroy(profile) {
            return $http.delete('/auth/api/accounts/' + profile.id + '/');
        }

        function get(username) {
            return $http.get('/auth/api/accounts/' + username + '/');
        }

        function update(profile) {
            return $http.put('/auth/api/accounts/' + profile.username + '/', profile);
        }
    }
})();