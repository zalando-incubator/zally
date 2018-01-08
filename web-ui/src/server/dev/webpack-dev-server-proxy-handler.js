'use strict';

const request = require('request');
const path = require('path');
const logger = require('../../server/logger');

module.exports = function({ publicPath, protocol, host, port }) {
  /* eslint-disable no-console */
  return function(req, res, next) {
    const segment = path.join(publicPath, req.url);
    const url = `${protocol}://${host}:${port}${segment}`;
    const proxyReq = request(url);
    logger.debug(`proxying request to webpack dev server: ${url}`);

    proxyReq.on('response', response => {
      if (response.statusCode >= 400) {
        logger.warn(
          `webpack dev server could not handle ${url}, call "next" middleware`
        );
        next();
        return;
      }
      proxyReq.pipe(res);
    });
  };
};
