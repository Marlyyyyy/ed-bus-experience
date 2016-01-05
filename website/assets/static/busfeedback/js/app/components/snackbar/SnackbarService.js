
(function ($, _) {
    'use strict';

    angular
    .module('runnerapp.snackbar.services')
    .factory('Snackbar', Snackbar);

    function Snackbar() {

        return {
            error: error,
            show: show
        };

        function _snackbar(content, options) {
            options = _.extend({ timeout: 3000 }, options);
            options.content = content;

            $.snackbar(options);
        }

        function error(content, options) {
            _snackbar('Error: ' + content, options);
        }

        function show(content, options) {
            _snackbar(content, options);
        }
    }
})($, _);