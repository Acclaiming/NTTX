/*
 * Copyright (c) 2018. Ernesto Castellotti <erny.castell@gmail.com>
 * This file is part of JTdlib.
 *
 *     JTdlib is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     JTdlib is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with JTdlib.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.ernytech.tdbot;

import it.ernytech.tdlib.TdApi;

/**
 * Interface for easy interaction with TDLib for user.
 */
public class UserClient extends EasyClient<UserClient> {

    /**
     * Creates a new UserClient.
     * @param phoneNumber The phone number of user.
     */
    public UserClient(final long phoneNumber) {

		super(new AuthorizationHandler() {

				@Override
				public void onAuthorizationStateWaitPhoneNumber(EasyClient easyClient) {

					easyClient.sendRaw(new TdApi.SetAuthenticationPhoneNumber(String.valueOf(phoneNumber),false,false));


				}

			});

    }

    public UserClient(final long phoneNumber,String firstName) {

		this(phoneNumber);

		this.firstName = firstName;

    }

    public UserClient(final long phoneNumber,String firstName,String lastName) {

		this(phoneNumber,firstName);
		
        this.lastName = lastName;
    }
}
