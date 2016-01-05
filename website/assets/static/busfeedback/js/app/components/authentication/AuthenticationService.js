
(function () {
    'use strict';

    angular
        .module('authentication.services')
        .factory('Authentication', Authentication);

    Authentication.$inject = ['$cookies', '$http'];

    function Authentication($cookies, $http) {

        var Authentication = {
            getAuthenticatedAccount: getAuthenticatedAccount,
            isAuthenticated: isAuthenticated,
            login: login,
            logout: logout,
            register: register,
            setAuthenticatedAccount: setAuthenticatedAccount,
            unauthenticate: unauthenticate
        };

        return Authentication;

        function register(password, username) {
            return $http.post('/auth/api/accounts/', {
                username: username,
                password: password
            }).then(registerSuccessFn, registerErrorFn);

            function registerSuccessFn(data, status, headers, config) {
                var response = data.data;
                Authentication.setAuthenticatedAccount(response.token, response.username)
            }

            function registerErrorFn(data, status, headers, config) {
                console.error('Registration failed! ' + status);
            }
        }

        function login(username, password) {
            return $http.post('/auth/api/login/', {
                username: username,
                password: password
            }).then(loginSuccessFn, loginErrorFn);

            function loginSuccessFn(data, status, headers, config) {
                var response = data.data;
                Authentication.setAuthenticatedAccount(response.token, response.username);
                window.location = 'home';
            }

            function loginErrorFn(data, status, headers, config) {
                console.error('Login failed!' + status);
            }
        }

        function logout() {
            Authentication.unauthenticate();
        }

        function getAuthenticatedAccount() {
            if (!$cookies.get('username')) {
                console.log("User is not authenticated!");
                return;
            }

            return $cookies.get('username');
        }

        function isAuthenticated() {
            return !!$cookies.get('token');
        }


        function setAuthenticatedAccount(token, username) {

            // Set the expiration to 6 months
            var now = new Date();
            var expiry = new Date(now.getFullYear(), now.getMonth()+6, now.getDate());

            // Store the token
            $cookies.put('token', token,{
              expires: expiry
            });

            // Set the token as a header
            $http.defaults.headers.common['Authorization'] = 'JWT ' + token;

            // Store the username
            $cookies.put('username', username,{
              expires: expiry
            });
        }

        function unauthenticate() {
            delete $cookies.remove('token');
            delete $cookies.remove('username');

            window.location = 'home';
        }
    }
})();