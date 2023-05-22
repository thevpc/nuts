Select rollno
FROM Student
WHERE ROWID <>
      (Select max(rowid) from Student b where rollno = b.rollno);
