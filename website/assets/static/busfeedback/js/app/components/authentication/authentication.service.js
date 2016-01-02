/**
 * Created by Marci on 25/08/2015.
 */


/**
* Authentication
* @namespace authentication.services
*/
(function () {
    'use strict';

    angular
        .module('authentication.services')
        .factory('Authentication', Authentication);

    Authentication.$inject = ['$cookies', '$http'];

    /**
    * @namespace Authentication
    * @returns {Factory}
    */
    function Authentication($cookies, $http) {
        /**
        * @name Authentication
        * @desc The Factory to be returned
        */
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

        ////////////////////

        /**
        * @name register
        * @desc Try to register a new user
        * @param {string} email The email entered by the user
        * @param {string} password The password entered by the user
        * @param {string} username The username entered by the user
        * @returns {Promise}
        * @memberOf authentication.services.Authentication
        */
        function register(email, password, username) {
            return $http.post('/api/v1/accounts/', {
                username: username,
                password: password,
                email: email
            }).then(registerSuccessFn, registerErrorFn);

            /**
            * @name registerSuccessFn
            * @desc Log the new user in
            */
            function registerSuccessFn(data, status, headers, config) {
                Authentication.login(email, password);
            }

            /**
            * @name registerErrorFn
            * @desc Log "Epic failure!" to the console
            */
            function registerErrorFn(data, status, headers, config) {
                console.error('Epic failure!');
            }
        }

        /**
        * @name login
        * @desc Try to log in with email `email` and password `password`
        * @param {string} email The email entered by the user
        * @param {string} password The password entered by the user
        * @returns {Promise}
        * @memberOf authentication.services.Authentication
        */
        function login(email, password) {
            return $http.post('/api/v1/auth/login/', {
                email: email,
                password: password
            }).then(loginSuccessFn, loginErrorFn);

            /**
            * @name loginSuccessFn
            * @desc Set the authenticated account and redirect to index
            */
            function loginSuccessFn(data, status, headers, config) {
                Authentication.setAuthenticatedAccount(data.data);
                window.location = 'home';
            }

            /**
            * @name loginErrorFn
            * @desc Log "Epic failure!" to the console
            */
            function loginErrorFn(data, status, headers, config) {
                console.error('Epic failure!');
            }
        }

        /**
        * @name logout
        * @desc Try to log the user out
        * @returns {Promise}
        * @memberOf authentication.services.Authentication
        */
        function logout() {
            return $http.post('/api/v1/auth/logout/')
                .then(logoutSuccessFn, logoutErrorFn);

            /**
            * @name logoutSuccessFn
            * @desc Unauthenticate and redirect to index with page reload
            */
            function logoutSuccessFn(data, status, headers, config) {
                Authentication.unauthenticate();

                window.location = 'home';
            }

            /**
            * @name logoutErrorFn
            * @desc Log "Epic failure!" to the console
            */
            function logoutErrorFn(data, status, headers, config) {
                console.error('Epic failure!');
            }
        }

        /**
        * @name getAuthenticatedAccount
        * @desc Return the currently authenticated account
        * @returns {object|undefined} Account if authenticated, else `undefined`
        * @memberOf authentication.services.Authentication
        */
        function getAuthenticatedAccount() {
            if (!$cookies.get('authenticatedAccount')) {
                return;
            }

            return JSON.parse($cookies.get('authenticatedAccount'));
        }

        /**
        * @name isAuthenticated
        * @desc Check if the current user is authenticated
        * @returns {boolean} True is user is authenticated, else false.
        * @memberOf authentication.services.Authentication
        */
        function isAuthenticated() {
            return !!$cookies.get('authenticatedAccount');
        }

        /**
        * @name setAuthenticatedAccount
        * @desc Stringify the account object and store it in a cookie
        * @param {Object} account The account object to be stored
        * @returns {undefined}
        * @memberOf authentication.services.Authentication
        */
        function setAuthenticatedAccount(account) {
            var now = new Date(),
            // this will set the expiration to 6 months
            exp = new Date(now.getFullYear(), now.getMonth()+6, now.getDate());

            $cookies.put('authenticatedAccount', JSON.stringify(account),{
              expires: exp
            });
        }

        /**
        * @name unauthenticate
        * @desc Delete the cookie where the user object is stored
        * @returns {undefined}
        * @memberOf authentication.services.Authentication
        */
        function unauthenticate() {
            delete $cookies.remove('authenticatedAccount');
        }
    }
})();