abstract class JobConstraint {
  const JobConstraint();
  Map<String, dynamic> serialize();
  String getName();
}

class BackoffCriteria extends JobConstraint {
  static const BACKOFF_POLICY_LINEAR = 0x00000000;
  static const BACKOFF_POLICY_EXPONENTIAL = 0x00000001;
  static const DEFAULT_INITIAL_BACKOFF_MILLIS = 0x0000000000007530;
  static const MAX_BACKOFF_DELAY_MILLIS = 0x000000000112a880;

  final int initialBackoffMillis;
  final int backoffPolicy;
  const BackoffCriteria({this.initialBackoffMillis, this.backoffPolicy});

  Map<String, dynamic> serialize() {
    return {
      "initialBackoffMillis": this.initialBackoffMillis,
      "backoffPolicy": this.backoffPolicy
    };
  }

  String getName() => "BackoffCriteria";
}

class PersistentAcrossReboots extends JobConstraint {
  final bool isPersistent;
  const PersistentAcrossReboots({this.isPersistent});

  Map<String, dynamic> serialize() {
    return { "isPersistent": this.isPersistent };
  }

  String getName() => "PersistentAcrossReboots";
}

class RequiredNetworkType extends JobConstraint {
  static const NETWORK_TYPE_ANY = 1;
  static const NETWORK_TYPE_CELLULAR = 2;
  static const NETWORK_TYPE_METERED = 4;
  static const NETWORK_TYPE_NONE = 0;
  static const NETWORK_TYPE_UNMETERED = 2;
  static const NETWORK_TYPE_NOT_ROAMING = 3;
  final int requiredType;
  const RequiredNetworkType({this.requiredType});

  Map<String, dynamic> serialize() {
    return { "requiredType": this.requiredType };
  }

  String getName() => "RequiredNetworkType";
}

class RequiresDeviceIdle extends JobConstraint {
  const RequiresDeviceIdle();
  Map<String, dynamic> serialize() {
    return {};
  }
  String getName() => "RequiresDeviceIdle";
}

class RequiresStorageNotLow extends JobConstraint {
  const RequiresStorageNotLow();
  Map<String, dynamic> serialize() {
    return {};
  }
  String getName() => "RequiresStorageNotLow";
}

class RequiresCharging extends JobConstraint {
  const RequiresCharging();
  Map<String, dynamic> serialize() {
    return {};
  }
  String getName() => "RequiresCharging";
}

