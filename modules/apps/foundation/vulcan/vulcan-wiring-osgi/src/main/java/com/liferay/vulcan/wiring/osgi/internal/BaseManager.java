/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.vulcan.wiring.osgi.internal;

import com.liferay.vulcan.wiring.osgi.GenericUtil;

import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author Alejandro Hernández
 */
public abstract class BaseManager<T> {

	public BaseManager() {
		Bundle bundle = FrameworkUtil.getBundle(BaseManager.class);

		_bundleContext = bundle.getBundleContext();
	}

	protected <U> Class<U> addService(
		ServiceReference<T> serviceReference, Class<T> clazz) {

		T service = _bundleContext.getService(serviceReference);

		Class<U> genericClass = GenericUtil.getGenericClass(service, clazz);

		_services.computeIfAbsent(
			genericClass.getName(), name -> new TreeSet<>());

		TreeSet<ServiceReferenceServiceTuple<T>> serviceReferenceServiceTuples =
			_services.get(genericClass.getName());

		ServiceReferenceServiceTuple<T> serviceReferenceServiceTuple =
			new ServiceReferenceServiceTuple<>(serviceReference, service);

		serviceReferenceServiceTuples.add(serviceReferenceServiceTuple);

		return genericClass;
	}

	protected <U> Optional<T> getServiceOptional(Class<U> clazz) {
		TreeSet<ServiceReferenceServiceTuple<T>> serviceReferenceServiceTuples =
			_services.get(clazz.getName());

		Optional<TreeSet<ServiceReferenceServiceTuple<T>>> optional =
			Optional.ofNullable(serviceReferenceServiceTuples);

		return optional.filter(
			treeSet -> !treeSet.isEmpty()
		).map(
			TreeSet::first
		).map(
			ServiceReferenceServiceTuple::getService
		);
	}

	protected <U> Class<U> removeService(
		ServiceReference<T> serviceReference, Class<T> clazz) {

		T service = _bundleContext.getService(serviceReference);

		Class<U> genericClass = GenericUtil.getGenericClass(service, clazz);

		TreeSet<ServiceReferenceServiceTuple<T>> serviceReferenceServiceTuples =
			_services.get(genericClass.getName());

		serviceReferenceServiceTuples.removeIf(
			serviceReferenceServiceTuple -> {
				if (serviceReferenceServiceTuple.getService() == service) {
					return true;
				}

				return false;
			});

		return genericClass;
	}

	private final BundleContext _bundleContext;
	private final Map<String, TreeSet<ServiceReferenceServiceTuple<T>>>
		_services = new ConcurrentHashMap<>();

	private static class ServiceReferenceServiceTuple<T>
		implements Comparable<ServiceReferenceServiceTuple> {

		public ServiceReferenceServiceTuple(
			ServiceReference<T> serviceReference, T service) {

			_serviceReference = serviceReference;
			_service = service;
		}

		@Override
		public int compareTo(
			ServiceReferenceServiceTuple serviceReferenceServiceTuple) {

			return _serviceReference.compareTo(
				serviceReferenceServiceTuple._serviceReference);
		}

		public T getService() {
			return _service;
		}

		private final T _service;
		private final ServiceReference<T> _serviceReference;

	}

}