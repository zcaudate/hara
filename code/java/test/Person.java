package test;

public class Person{
  int age;
  String name;
  Pet[] pets;
  public Person(String name, int age, Pet[] pets){
    this.name = name;
    this.age = age;
    this.pets = pets;
  };
  public int getAge(){return age;}
  public String getName(){return name;}
  public Pet[] getPets(){return pets;}
}
