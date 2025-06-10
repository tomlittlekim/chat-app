// MongoDB 초기화 스크립트
print('MongoDB 초기화 시작...');

// chatdb 데이터베이스로 전환
db = db.getSiblingDB('chatdb');

// 사용자 생성
db.createUser({
  user: 'chatuser',
  pwd: 'chatpassword',
  roles: [
    {
      role: 'readWrite',
      db: 'chatdb'
    }
  ]
});

// 컬렉션 생성 및 인덱스 설정
db.createCollection('users');
db.createCollection('chatRooms');
db.createCollection('messages');

// 인덱스 생성
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "status": 1 });

db.chatRooms.createIndex({ "name": 1 });
db.chatRooms.createIndex({ "type": 1 });
db.chatRooms.createIndex({ "isActive": 1 });

db.messages.createIndex({ "roomId": 1, "timestamp": -1 });
db.messages.createIndex({ "senderId": 1 });
db.messages.createIndex({ "type": 1 });

print('MongoDB 초기화 완료!'); 