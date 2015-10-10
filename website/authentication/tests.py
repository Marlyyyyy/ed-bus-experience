from django.test import TestCase
from django.contrib.auth.models import User
from django.test import Client
import json
from django.contrib.auth import SESSION_KEY


class AuthenticationViewTestCase(TestCase):

    client = Client()
    response = None

    def setUp(self):
        self.response = self.client.post("/auth/api/accounts/", {"username": "Heffalumps", "password": "Woozles"})

    def test_create_account(self):
        response_content = json.loads(self.response.content.decode('utf-8'))
        self.assertEqual("Heffalumps", response_content["username"], "Response should contain the username.")
        self.assertEqual("Woozles", response_content["password"], "Response should contain the password.")

        latest_account = User.objects.latest('date_joined')
        self.assertEqual("Heffalumps", latest_account.username, "The username must be present in the database.")

    def test_login(self):
        self.assertTrue(SESSION_KEY not in self.client.session, "The user should not be logged in yet.")

        self.client.post("/auth/api/login/", {"username": "Heffalumps", "password": "Woozles"})
        self.assertTrue(SESSION_KEY in self.client.session, "The user should now be logged in.")

    def test_logout(self):
        self.client.post("/auth/api/login/", {"username": "Heffalumps", "password": "Woozles"})

        self.client.post("/auth/api/logout/")
        self.assertTrue(SESSION_KEY not in self.client.session, "The user should not be logged in anymore.")

    def test_unauthorised_login(self):
        response = self.client.post("/auth/api/login/", {"username": "Mango", "password": "Apple"})
        self.assertEqual(response.status_code, 401, "There shouldn't exist such a user.")

        response = self.client.post("/auth/api/login/", {"username": "Heffalumps", "password": "Apple"})
        self.assertEqual(response.status_code, 401, "The password should be incorrect.")

    def test_get_token(self):
        response = self.client.post("/auth/api/get_token/", {"username": "Heffalumps", "password": "Woozles"})
        self.assertEqual(response.status_code, 200, "The token should be successfully returned.")

