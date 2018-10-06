package test;

public class Cat implements Pet
{
    String name;

    public Cat(String name){
        this.name = name;
    }
    
    public String getName(){return name;}
    public String getSpecies(){return "cat";}
    public void setName(String name){
      this.name = name;
    }
}