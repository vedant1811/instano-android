/**
 * each class fetches model objects using the Observable pattern
 * each is a singleton to be accessed by ClassName.controller()
 *
 * Each class takes care of Caching (both in memory and storage) and fetching from server.
 * It is best not to keep copies of model objects emitted from here
 */
package com.instano.retailer.instano.application.controller;