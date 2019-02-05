package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link android.content.pm.SigningInfo}.
 */
@Implements(value = SigningInfo.class, minSdk = P)
public class ShadowSigningInfo {
  private Signature[] signatures;
  private Signature[] pastSigningCertificates;

  /**
   * Set the current Signatures for this package. If signatures has a size greater than 1,
   * {@link #hasMultipleSigners} will be true and {@link #getSigningCertificateHistory} will return
   * null.
   */
  public void setSignatures(Signature[] signatures) {
    this.signatures = signatures;
  }

  /**
   * Sets the history of Signatures for this package.
   */
  public void setPastSigningCertificates(Signature[] pastSigningCertificates) {
    this.pastSigningCertificates = pastSigningCertificates;
  }

  /**
   * Although relatively uncommon, packages may be signed by more than one signer, in which case
   * their identity is viewed as being the set of all signers, not just any one.
   */
  @Implementation
  protected boolean hasMultipleSigners() {
    return signatures != null && signatures.length > 1;
  }

  /**
   * APK Signature Scheme v3 enables packages to provide a proof-of-rotation record that the
   * platform verifies, and uses, to allow the use of new signing certificates. This is only
   * available to packages that are not signed by multiple signers. In the event of a change to a
   * new signing certificate, the package's past signing certificates are presented as well. Any
   * check of a package's signing certificate should also include a search through its entire
   * signing history, since it could change to a new signing certificate at any time.
   */
  @Implementation
  protected boolean hasPastSigningCertificates() {
    return signatures != null && pastSigningCertificates != null;
  }

  /**
   * Returns the signing certificates this package has proven it is authorized to use. This includes
   * both the signing certificate associated with the signer of the package and the past signing
   * certificates it included as its proof of signing certificate rotation. This method is the
   * preferred replacement for the {@code GET_SIGNATURES} flag used with {@link
   * PackageManager#getPackageInfo(String, int)}. When determining if a package is signed by a
   * desired certificate, the returned array should be checked to determine if it is one of the
   * entries.
   *
   * <p><note> This method returns null if the package is signed by multiple signing certificates,
   * as opposed to being signed by one current signer and also providing the history of past signing
   * certificates. {@link #hasMultipleSigners()} may be used to determine if this package is signed
   * by multiple signers. Packages which are signed by multiple signers cannot change their signing
   * certificates and their {@code Signature} array should be checked to make sure that every entry
   * matches the looked-for signing certificates. </note>
   */
  @Implementation
  protected Signature[] getSigningCertificateHistory() {
    if (hasMultipleSigners()) {
      return null;
    } else if (!hasPastSigningCertificates()) {
      // this package is only signed by one signer with no history, return it
      return signatures;
    } else {
      // this package has provided proof of past signing certificates, include them
      return pastSigningCertificates;
    }
  }

  /**
   * Returns the signing certificates used to sign the APK contents of this application. Not
   * including any past signing certificates the package proved it is authorized to use. <note> This
   * method should not be used unless {@link #hasMultipleSigners()} returns true, indicating that
   * {@link #getSigningCertificateHistory()} cannot be used, otherwise {@link
   * #getSigningCertificateHistory()} should be preferred. </note>
   */
  @Implementation
  protected Signature[] getApkContentsSigners() {
    return signatures;
  }
}
