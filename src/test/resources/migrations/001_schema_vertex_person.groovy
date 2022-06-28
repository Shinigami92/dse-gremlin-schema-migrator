// Example code taken from:
// https://docs.datastax.com/en/dse/6.8/dse-dev/datastax_enterprise/graph/using/examineSchema.html

schema.vertexLabel('person').
    partitionBy('person_id', Uuid).
    property('cal_goal', Int).
    property('gender', Varchar).
    property('name', Varchar).
    property('badge', mapOf(Varchar, Date)).
    property('country', listOf(tupleOf(Varchar, Date, Date))).
    property('macro_goal', listOf(Int)).
    property('nickname', setOf(Varchar)).
create()
