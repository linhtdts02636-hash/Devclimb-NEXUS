import {
  browserLocalPersistence,
  browserSessionPersistence,
  createUserWithEmailAndPassword,
  GithubAuthProvider,
  GoogleAuthProvider,
  sendEmailVerification,
  sendPasswordResetEmail,
  setPersistence,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  updateProfile,
} from 'firebase/auth'
import { auth } from '../../config/firebase'

export const SocialProvider = Object.freeze({
  GOOGLE: 'google',
  GITHUB: 'github',
})

const PASSWORD_PROVIDER_ID = 'password'

function createSocialAuthProvider(socialProvider) {
  if (socialProvider === SocialProvider.GOOGLE) {
    return new GoogleAuthProvider()
  }
  const githubProvider = new GithubAuthProvider()
  githubProvider.addScope('user:email')
  return githubProvider
}

function applyPersistence(rememberMe) {
  return setPersistence(auth, rememberMe ? browserLocalPersistence : browserSessionPersistence)
}

export async function signInWithEmail({ email, password, rememberMe }) {
  await applyPersistence(rememberMe)
  await signInWithEmailAndPassword(auth, email, password)
}

export async function signUpWithEmail({ displayName, email, password, rememberMe }) {
  await applyPersistence(rememberMe)
  const credential = await createUserWithEmailAndPassword(auth, email, password)
  await updateProfile(credential.user, { displayName })
  await sendEmailVerification(credential.user)
}

export async function signInWithSocialProvider({ socialProvider, rememberMe }) {
  await applyPersistence(rememberMe)
  await signInWithPopup(auth, createSocialAuthProvider(socialProvider))
}

export function resendVerificationEmail() {
  return sendEmailVerification(auth.currentUser)
}

export function sendPasswordReset(email) {
  return sendPasswordResetEmail(auth, email)
}

export function signOutCurrentUser() {
  return signOut(auth)
}

export function requiresEmailVerification(firebaseUser) {
  const usesPassword = firebaseUser.providerData.some((provider) => provider.providerId === PASSWORD_PROVIDER_ID)
  return usesPassword && !firebaseUser.emailVerified
}
