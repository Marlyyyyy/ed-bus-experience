/**
 * Created by Marci on 28/08/2015.
 */

(function () {
    'use strict';

    angular
        .module('authentication', [
            'authentication.controllers',
            'authentication.services'
        ]);

    angular
        .module('authentication.controllers', []);

    angular
        .module('authentication.services', ['ngCookies']);
})();