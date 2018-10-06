package test;

public class PersonBuilder{
  int age;
  String name;
  Pet[] pets;
  public PersonBuilder(){};
  public PersonBuilder setAge(int age){this.age = age; return this;}
  public PersonBuilder setName(String name){this.name = name; return this;}
  public PersonBuilder setPets(Pet[] pets){this.pets = pets; return this;}
  public Person build(){
    return new Person(this.name, this.age, this.pets);
  }
}

// (-> (test.PersonBuilder.) (.setAge 1) (.setName "Chris") (.build))
