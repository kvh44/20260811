const appDb = db.getSiblingDB("appdb");
const users = appDb.getCollection("mongoClients");

users.createIndex(
  { email: 1 },
  { name: "email_unique", unique: true }
);

const operations = Array.from({ length: 100 }, (_, index) => {
  const number = index + 1;
  const email = `user${number}@example.com`;

  return {
    updateOne: {
      filter: { email },
      update: {
        $set: {
          name: `User ${number}`,
          email
        }
      },
      upsert: true
    }
  };
});

const result = users.bulkWrite(operations, { ordered: true });

printjson({
  database: appDb.getName(),
  collection: users.getName(),
  users: users.countDocuments({}),
  inserted: result.upsertedCount,
  updated: result.modifiedCount
});
