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

package com.liferay.apio.architect.sample.internal.resource;

import com.liferay.apio.architect.pagination.PageItems;
import com.liferay.apio.architect.pagination.Pagination;
import com.liferay.apio.architect.representor.Representor;
import com.liferay.apio.architect.resource.CollectionResource;
import com.liferay.apio.architect.routes.CollectionRoutes;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.apio.architect.sample.internal.form.PersonForm;
import com.liferay.apio.architect.sample.internal.model.Person;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

import org.osgi.service.component.annotations.Component;

/**
 * Provides all the information necessary to expose <a
 * href="http://schema.org/Person">Person </a> resources through a web API. The
 * resources are mapped from the internal {@link Person} model.
 *
 * @author Alejandro Hernández
 */
@Component(immediate = true)
public class PersonCollectionResource
	implements CollectionResource<Person, Long> {

	@Override
	public CollectionRoutes<Person> collectionRoutes(
		CollectionRoutes.Builder<Person> builder) {

		return builder.addGetter(
			this::_getPageItems
		).addCreator(
			this::_addPerson, PersonForm::buildForm
		).build();
	}

	@Override
	public String getName() {
		return "people";
	}

	@Override
	public ItemRoutes<Person> itemRoutes(
		ItemRoutes.Builder<Person, Long> builder) {

		return builder.addGetter(
			this::_getPerson
		).addRemover(
			this::_deletePerson
		).addUpdater(
			this::_updatePerson, PersonForm::buildForm
		).build();
	}

	@Override
	public Representor<Person, Long> representor(
		Representor.Builder<Person, Long> builder) {

		return builder.types(
			"Person"
		).identifier(
			Person::getPersonId
		).addDate(
			"birthDate", Person::getBirthDate
		).addString(
			"address", Person::getAddress
		).addString(
			"email", Person::getEmail
		).addString(
			"familyName", Person::getLastName
		).addString(
			"givenName", Person::getFirstName
		).addString(
			"image", Person::getAvatar
		).addString(
			"jobTitle", Person::getJobTitle
		).addString(
			"name", Person::getFullName
		).build();
	}

	private Person _addPerson(PersonForm personForm) {
		return Person.addPerson(
			personForm.getAddress(), personForm.getImage(),
			personForm.getBirthDate(), personForm.getEmail(),
			personForm.getGivenName(), personForm.getJobTitle(),
			personForm.getFamilyName());
	}

	private void _deletePerson(Long personId) {
		Person.deletePerson(personId);
	}

	private PageItems<Person> _getPageItems(Pagination pagination) {
		List<Person> persons = Person.getPeople(
			pagination.getStartPosition(), pagination.getEndPosition());
		int count = Person.getPeopleCount();

		return new PageItems<>(persons, count);
	}

	private Person _getPerson(Long personId) {
		Optional<Person> optional = Person.getPerson(personId);

		return optional.orElseThrow(
			() -> new NotFoundException("Unable to get person " + personId));
	}

	private Person _updatePerson(Long personId, PersonForm personForm) {
		Optional<Person> optional = Person.updatePerson(
			personForm.getAddress(), personForm.getImage(),
			personForm.getBirthDate(), personForm.getEmail(),
			personForm.getGivenName(), personForm.getJobTitle(),
			personForm.getFamilyName(), personId);

		return optional.orElseThrow(
			() -> new NotFoundException("Unable to get person " + personId));
	}

}