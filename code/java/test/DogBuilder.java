package test;

public class DogBuilder{
  String name;
  public DogBuilder(){};
  public DogBuilder setName(String name){this.name = name; return this;}
  public Dog build(){
    return new Dog(this.name, "dog");
  }
}

// (-> (test.PersonBuilder.) (.setAge 1) (.setName "Chris") (.build))
