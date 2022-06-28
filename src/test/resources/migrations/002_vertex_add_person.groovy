// Example code taken from:
// https://docs.datastax.com/en/dse/6.8/dse-dev/datastax_enterprise/graph/using/insertTraversalAPI.html?hl=inserting%2Cdata%2Cgraph%2Ctraversal%2Capi

g.addV('person').
    property('person_id', 'adb8744c-d015-4d78-918a-d7f062c59e8f' as UUID).
    property('name', 'Simone BECK').
    property('gender','F').
    property('nickname', ['Simca', 'Simone'] as Set).
    property('country', [['France', '1904-07-07' as LocalDate, '1991-12-20' as LocalDate] as Tuple])
