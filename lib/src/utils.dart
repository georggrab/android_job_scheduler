import './constraints.dart';

Map<String, Map<String, dynamic>> serializeConstraints(List<JobConstraint> constraints) {
  if (constraints == null) {
    return null;
  }
  return new Map.fromEntries(
      constraints.map((JobConstraint constraint) => MapEntry(constraint.getName(), constraint.serialize()))
  );
}
